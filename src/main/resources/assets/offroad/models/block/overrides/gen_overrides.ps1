$colors = @(
    "black", "blue", "brown", "copper", "cyan", "gray", "green",
    "light_blue", "light_gray", "lime", "magenta", "orange", "pink",
    "purple", "red", "white", "yellow"
)

foreach ($color in $colors) {
    $json = @"
{
	"parent": "block/block",
	"loader": "neoforge:obj",
	"flip_v": true,
	"model": "create:models/block/valve_handle/valve_handle.obj",
	"textures": {
		"3": "create:block/valve_handle/valve_handle_$color",
		"particle": "#3"
	},
	"display": {
		"fixed": {
			"rotation": [
				270,
				0,
				0
			],
			"translation": [
				0,
				0,
				-2
			],
			"scale": [
				0.5,
				0.5,
				0.5
			]
		}
	}
}
"@
    $filename = "${color}_valve_handle.json"
    Set-Content -Path $filename -Value $json -Encoding UTF8
    Write-Host "generated: $filename"
}

Write-Host "`nDone. $($colors.Count) files created."